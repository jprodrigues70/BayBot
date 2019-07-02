## BayBot

Esse não é um robô campeão, nem mesmo "inteligente", mas às vezes consegue ser esperto haha.

Isto é apenas um exemplo da aplicação da fórmula de Bayes no robocode.

```
p(h|d) = (P(d|h)*P(h)) / P(d)
```
Onde:

- P(h): a priori (probabilidade da hipótese h antes de ver qualquer dado);
- P(d|h): likelihood (verossimilhância);
- P(d) = ∑ĸ P(d|h)P(h): probabilidade marginal;
- P(h|d): a posteriori (probabilidade da hipótese h depois de ter visto o dado d).

A fórmula foi utilizada para responder às seguintes perguntas:

"Qual a força de tiro que eu mais consigo acertar no meu adversário quando estou perto dele? E quando estou longe dele?"

Para começar, disparamos, de forma aleatória, as intensidades de tiros predefinidas.

Foram utilizadas 3 intensidades de tiro. Tiro fraco, tiro médio e tiro forte.

Foram utilizadas duas classes: Hit (Tiros acertados) e Miss (Tiros errados). Na primeira etapa, agrupamos os tiros disparados em Hit e Miss.

Depois calculamos a probabilidade de um tiro ser Hit ou Miss.

```
pHit = totalHit / totalShots;
pMiss = totalMiss / totalShots;
```

Depois, contabilizamos quantos tiros de cada intensidade ocorreram. Contabilizamos também quantos tiros foram de longe e quantos foram de perto.

Em seguida, calculamos a probabilidade de cada insensidade em Hit e em Miss.

```
pHit_t1 = totalHit_t1 / totalHit;
pHit_t2 = totalHit_t2 / totalHit;
pHit_t3 = totalHit_t3 / totalHit;
```

```
pMiss_t1 = totalMiss_t1 / totalMiss;
pMiss_t2 = totalMiss_t2 / totalMiss;
pMiss_t3 = totalMiss_t3 / totalMiss;
```

E a probabilidade de um tiro qualquer ter sido de longe ou perto em Hit e em Miss.

```
pHit_near = totalHit_Near / totalHit;
pHit_far = totalHit_far / totalHit;
```

```
pMiss_near = totalMiss_Near / totalMiss;
pMiss_far = totalMiss_far / totalMiss;
```


Em seguida, calculamos a probabilidade de cada intensidade de tiro ser de perto ou de longe e acertar o oponente.

```
    pHit_t1_near = pHit_t1 * pHit_near * pHit;
    pHit_t2_near = pHit_t2 * pHit_near * pHit;
    pHit_t3_near = pHit_t3 * pHit_near * pHit;
```

```
    pHit_t1_far = pHit_t1 * pHit_far * pHit;
    pHit_t2_far = pHit_t2 * pHit_far * pHit;
    pHit_t3_far = pHit_t3 * pHit_far * pHit;
```

Em seguida, a mesma probabilidade, só que para erro.

```
    pMiss_t1_near = pMiss_t1 * pMiss_near * pMiss;
    pMiss_t2_near = pMiss_t2 * pMiss_near * pMiss;
    pMiss_t3_near = pMiss_t3 * pMiss_near * pMiss;
```

```
    pMiss_t1_far = pMiss_t1 * pMiss_far * pMiss;
    pMiss_t2_far = pMiss_t2 * pMiss_far * pMiss;
    pMiss_t3_far = pMiss_t3 * pMiss_far * pMiss;
```

Como para nossa estratégia só interessam os acertos, calculamos a probabilidade final de acertos para cada tiro.

```
 pFinal_t1_near = pHit_t1_near / max((pHit_t1_near + pMiss_t1_near), 1);
 pFinal_t2_near = pHit_t2_near / max((pHit_t2_near + pMiss_t2_near), 1);
 pFinal_t3_near = pHit_t3_near / max((pHit_t3_near + pMiss_t3_near), 1);
```

```
 pFinal_t1_far = pHit_t1_far / max((pHit_t1_far + pMiss_t1_far), 1);
 pFinal_t2_far = pHit_t2_far / max((pHit_t2_far + pMiss_t2_far), 1);
 pFinal_t3_far = pHit_t3_far / max((pHit_t3_far + pMiss_t3_far), 1);
```


A intensidade para tiros de perto é dada por:

```
nearShot = findItemWithHighProbability(pFinal_t1_near, pFinal_t2_near, pFinal_t3_near)
```

E para tiros de longe:

```
farShot = findItemWithHighProbability(pFinal_t1_far, pFinal_t2_far, pFinal_t3_far)
```
