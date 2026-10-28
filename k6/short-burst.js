import http from 'k6/http';
import {check} from 'k6';

export const options = {
    scenarios: {
        short_burst: {
            executor: 'shared-iterations',
            vus: 1,
            iterations: 150,
            maxDuration: '30s',
        },
    },
};

export default function() {
    const res = http.get('http://localhost:8080/ping', {headers: {'X-Client-Id': `short-burst-client-${__ENV.REP}`}});
    check(res, {
        'status is 200': (r) => r.status === 200,
    });
}