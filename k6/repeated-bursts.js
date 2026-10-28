import http from 'k6/http';
import {check, sleep} from 'k6';

export const options = {
    scenarios: {
        repeated_bursts: {
            executor: 'shared-iterations',
            vus: 1,
            iterations: 1,
            maxDuration: '60s',
        },
    },
};

export default function() {
    for(let burst = 0; burst < 10; burst ++) {
        for(let i = 0; i < 50; i++){
            const res = http.get('http://localhost:8080/ping');
            check(res, {
                'repeated burst status is 200' : (r) => r.status === 200,
            });
            sleep(1/50);
        }
        sleep(2);
    }
}