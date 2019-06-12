(ns clojure-experiments.aws.ops
  (:require [cheshire.core :as json]))

;; Run aws --profile jumar ec2 describe-security-groups > security-groups.txt
;; and then process the result with this code:
(def sec-groups (-> (slurp "/Users/jumar/workspace/docker/docker-on-aws/security-groups.txt")
                    (json/parse-string)
                    (get "SecurityGroups")))

;; one security group data may look like this
;; "IpPermissionsEgress" = []
;; "Description" = "AWS ElasticBeanstalk Security Group"
;; "IpPermissions" = [ { "PrefixListIds" [], ... } ]
;; "GroupName" = "elasticbeanstalk-default"
;; "OwnerId" = "823153443126"
;; "GroupId" = "sg-5e8ce537"

(def jcloud-sec-groups-ids
  (->> sec-groups
       (filterv #(clojure.string/starts-with?
                  (get % "GroupName")
                  "jclouds"))
       (mapv #(get % "GroupId"))))


;; write out just the IDs to the file so you can delete them via aws-cli
;;   cat jcloud-security-groups-ids.txt | while read line; do aws --profile jumar ec2 delete-security-group --group-id "$line"; done

(spit "/Users/jumar/workspace/docker/docker-on-aws/jcloud-security-groups-ids.txt"
      (clojure.string/join "\n" jcloud-sec-groups-ids))
