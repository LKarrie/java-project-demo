## Timed

Micrometer 指标类型 Timer, Counter, Gauge, DistributionSummary, LongTaskTimer, FunctionCounter, FunctionTimer, TimeGauge
Prometheus 指标类型 Counter, Gauge, Histogram, Summary

Timed 只是Micrometer包装出来的注解 通过Timed注解参数的不同生成不同类型Micrometer指标 最后适配成Prometheus可以识别的指标