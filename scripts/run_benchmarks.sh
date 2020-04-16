#!/bin/bash

if [[ ! -e benchmarking_results ]]; then
    mkdir benchmarking_results
elif [[ ! -d benchmarking_results ]]; then
    rm benchmarking_results
    mkdir benchmarking_results
fi

node ./scripts/run_benchmarks.js
