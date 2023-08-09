// See https://k6.io/docs/get-started/running-k6/
import http from 'k6/http';
import { sleep } from 'k6';

export default function () {
  // http.get('https://test.k6.io');
   http.get('http://localhost:3000/api');
   sleep(0.1 * Math.random());
}
