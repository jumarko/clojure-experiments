(ns clojure-experiments.security.encryption
  "Basic encryption utils for easy encryption.

  This is a demonstration how passwords could be encrypted in an application's config.
  See https://github.com/empear-analytics/codescene-enterprise-pm-jira/blob/master/src/codescene_enterprise_pm_jira/config.clj
  for a real example.

  Support for optional encryption of passwords stored in the plugin's configuration.
  Uses master encryption key set in the `encryption-key-env-var`.

  See buddy documentation:
  - http://funcool.github.io/buddy-core/latest/#ciphers
  - http://funcool.github.io/buddy-core/latest/#high-level-encryption-schemes.

  You may also check https://git.xyser.com/Test/metabase/blob/00e2d3ec4d6299ab452f1f33f978e54566a876d9/src/metabase/util/encryption.clj
  for an inspiration.

  Note that we're not using CBC mode cipher since that was discontinued in TLS 1.3.
  As per suggestions in https://stackoverflow.com/questions/1220751/how-to-choose-an-aes-encryption-mode-cbc-ecb-ctr-ocb-cfb
  we use GCM instead.

  About managing 'initialization vectors' (nonces):
  - https://security.stackexchange.com/questions/17044/when-using-aes-and-cbc-is-it-necessary-to-keep-the-iv-secret"
  (:require
   [buddy.core
    [codecs :as bcodecs]
    [crypto :as bcrypto]
    [hash :as bhash]]
   [clojure.string :as string]))

(def encryption-algorithm {:algorithm :aes256-gcm})

;; if this var is set then we expect encrypted passwords
(def encryption-key-env-var "APP_ENCRYPTION_KEY")

;; Initialization vector (nonce) used for encryption
;; Despite the usual recommendations it's constant (either hard-coded or set in env var)
;; because we don't store passwords/nonces anywhere.
;; Since we only encrypt a few secrets it shouldn't make a big difference.
(def encryption-nonce-env-var "APP_ENCRYPTION_NONCE")
(def default-encryption-nonce "abcdefghijkl")

(defn encryption-key
  "Read optional 'master password' for encrypting/decrypting secrets from environment.
  Returns 32-byte hash as required by `encrypt-password` and `decrypt-password`."
  []
  (some->
   (System/getenv encryption-key-env-var)
   (bhash/sha256)))

(defn encryption-nonce
  []
  (let [nonce-str (or (System/getenv encryption-nonce-env-var)
                      default-encryption-nonce)]
    (bcodecs/str->bytes nonce-str)))

(defn- encrypt-password
  "Encrypts given password using AES 256 + GCM with given encryption key (32-byte hash)
  and returns hex-encoded version of the encrypted password.
  Use `decrypt-password` to obtain a plaintext-form from an encrypted version.
  See http://funcool.github.io/buddy-core/latest/#high-level-encryption-schemes.
  Note: GCM requires 32-byte encryption key and 12-byte nonce (iv)
  "
  [enc-key plaintext-password]
  (let [password-bytes (bcodecs/to-bytes plaintext-password)
        encrypted-bytes (bcrypto/encrypt password-bytes
                                         enc-key
                                         (encryption-nonce)
                                         encryption-algorithm)]
    (bcodecs/bytes->hex encrypted-bytes)))

(defn- decrypt-password
  "Decrypts the password previously encrypted via `encrypt-password`
  and returns its plaintext form as a string.
  Expects encrypted password to be passed as hex-encoded string (not bytes)."
  [enc-key encrypted-password-hex]
  (let [password-bytes (bcodecs/hex->bytes encrypted-password-hex)
        decrypted-bytes (bcrypto/decrypt password-bytes
                                         enc-key
                                         (encryption-nonce)
                                         encryption-algorithm)]
    (bcodecs/bytes->str decrypted-bytes)))

(defn encrypt-user-password
  "Higher-level version of `encrypt-password` using/checking the value of the env var
  and also providing error reporting."
  [report-error-fn plaintext-password]
  (if (string/blank? plaintext-password)
    (report-error-fn "Password cannot be empty!")
    (if-let [enc-key (encryption-key)]
      (encrypt-password enc-key plaintext-password)
      (report-error-fn (format "No encryption key set. Please, set the %s environment variable first!"
                               encryption-key-env-var)))))

(defn decrypt-secrets
  "Decrypts passwords stored in presumably encrypted form in the configuration
  and replaces the encrypted version with a plaintext-form.
  If encryption key isn't set in environment, returns the original app-config."
  [app-config]
  (if-let [enc-key (encryption-key)]
    (let [decrypt-fn (partial decrypt-password enc-key)]
      (-> app-config
          (update-in [:auth :jira :password] decrypt-fn)
          (update-in [:auth :service :password] decrypt-fn)))
    ;; no encryption key => asumming plaintext passwords
    app-config))

;;; set the 'APP_ENCRYPTION_KEY` first!
(comment

  (encrypt-user-password println "jirapass")
  ;; => "09701c541cde0598512e6c70030fd3c3df588cf077b3c655"
  (encrypt-user-password println "servicepass")
  ;; => "107c1c4305dc139b8262129e0572901d3581b22d580bff3c23ad26"

  (decrypt-password (encryption-key)  "09701c541cde0598512e6c70030fd3c3df588cf077b3c655")
  ;; => "jirapass"

  (decrypt-password (encryption-key)   "107c1c4305dc139b8262129e0572901d3581b22d580bff3c23ad26")
  ;; => "servicepass"

  (def my-config
    {:sync {:hour-interval 1},
     :auth
     {:service {:username "codescene-jira", :password "107c1c4305dc139b8262129e0572901d3581b22d580bff3c23ad26"},
      :jira
      {:base-uri "https://empear.atlassian.net",
       :username "juraj.martinka@empear.com",
       :password "09701c541cde0598512e6c70030fd3c3df588cf077b3c655"}},
     :projects
     [{:key "TEST",
       :cost-unit {:type "minutes"},
       :cost-field "timeoriginalestimate",
       :supported-work-types ["Bug" "Feature" "Refactoring" "Documentation"],
       :ticket-id-pattern "(TEST-\\d+)"}]})

  (decrypt-secrets my-config)

  ;; end
  )


