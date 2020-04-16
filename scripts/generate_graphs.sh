#!/bin/bash

if [[ ! -e benchmarking_results ]]; then
    npm run benchmark
elif [[ ! -d benchmarking_results ]]; then
    rm benchmarking_results
    npm run benchmark
fi

python3 scripts/plot_graphs.py
