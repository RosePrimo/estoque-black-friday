// 500 pessoas tentando comprar 1 unidade cada, 100 ao mesmo tempo.
import http from 'k6/http';
import { Counter } from 'k6/metrics';

const vendas = new Counter('vendas_aprovadas');
const recusas = new Counter('vendas_recusadas');
const erros = new Counter('vendas_com_erro');

const BASE = __ENV.BASE_URL || 'http://localhost:8080';
const PRODUTO = __ENV.PRODUTO || '2';

export const options = {
    scenarios: {
        black_friday: {
            executor: 'shared-iterations',
            vus: 100,          // 100 pessoas ao mesmo tempo
            iterations: 500,   // 500 tentativas de compra no total
        },
    },
};

export default function () {
    const res = http.post(
        `${BASE}/produtos/${PRODUTO}/comprar`,
        JSON.stringify({ quantidade: 1 }),
        { headers: { 'Content-Type': 'application/json' } }
    );
    if (res.status === 200) vendas.add(1);
    else if (res.status === 409) recusas.add(1);
    else erros.add(1);
}