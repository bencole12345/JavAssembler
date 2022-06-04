# GRADLE ?= ./gradlew
GRADLE = gradle
NODE ?= node
PYTHON ?= python3
EMPLUSPLUS ?= em++

# The JavAssembler compiler (an executable fat jar containing all dependencies)
FAT_JAR = build/libs/JavAssembler-1.0-all.jar
JAVASSEMBLER = java -jar $(FAT_JAR)

# Where all the outputs from JavAssembler will go
COMPILED_OUTPUTS_DIR = sample_programs_compiled

# Files for system tests
TESTS_SRCS = $(wildcard sample_programs/tests/*.java)
TESTS_WAT = $(COMPILED_OUTPUTS_DIR)/tests.wat

# Files for benchmarking
BENCHMARKS_SRCS_DIR = sample_programs/benchmarks
BENCHMARKS_JAVASSEMBLER_SRCS = $(wildcard $(BENCHMARKS_SRCS_DIR)/java/*.java)
BENCHMARKS_JAVASSEMBLER_WAT = $(COMPILED_OUTPUTS_DIR)/javassembler_benchmarks.wat
BENCHMARKS_CPP_SRCS = $(wildcard $(BENCHMARKS_SRCS_DIR)/cpp/*.cpp)
BENCHMARKS_CPP_JS = $(COMPILED_OUTPUTS_DIR)/cpp_benchmarks.js
BENCHMARKS_CPP_WAT = $(COMPILED_OUTPUTS_DIR)/cpp_benchmarks.wat
BENCHMARKS_RESULTS_DIR = benchmarking_results
BENCHMARKS_RESULTS_CSV = $(BENCHMARKING_RESULTS_DIR)/benchmarking_results.csv


.PHONY: build
build: $(FAT_JAR)

$(FAT_JAR):
	$(GRADLE) shadowJar

.PHONY: compile-tests
compile-tests: $(TESTS_WAT)

$(TESTS_WAT): $(FAT_JAR) $(TESTS_SRCS) $(COMPILED_OUTPUTS_DIR)
	$(JAVASSEMBLER) -i $(TESTS_SRCS) -o $(TESTS_WAT)

.PHONY: compile-benchmarks
compile-benchmarks: $(BENCHMARKS_JAVASSEMBLER_WAT) $(BENCHMARKS_CPP_JS) $(BENCHMARKS_CPP_WAT)

$(COMPILED_OUTPUTS_DIR):
	mkdir -p $(COMPILED_OUTPUTS_DIR)

$(BENCHMARKS_JAVASSEMBLER_WAT): $(FAT_JAR) $(BENCHMARKS_JAVASSEMBLER_SRCS) $(COMPILED_OUTPUTS_DIR)
	$(JAVASSEMBLER) -i $(BENCHMARKS_JAVASSEMBLER_SRCS) -o $(BENCHMARKS_JAVASSEMBLER_WAT)

# Generated automatically alongside the JS file
$(BENCHMARKS_CPP_WAT): $(BENCHMARKS_CPP_JS)

$(BENCHMARKS_CPP_JS): $(BENCHMARKS_CPP_SRCS)
	$(EMPLUSPLUS) $(BENCHMARKS_CPP_SRCS) \
		-s EXTRA_EXPORTED_RUNTIME_METHODS='["ccall", "cwrap"]' \
		-s ALLOW_MEMORY_GROWTH=1 \
		-o $(BENCHMARKS_CPP_JS)

.PHONY: test
test: unit-test system-test

.PHONY: unit-test
unit-test:
	$(GRADLE) test

.PHONY: system-test
system-test: $(TESTS_WAT)
	npm test

.PHONY: run-benchmarks
run-benchmarks: $(BENCHMARKS_RESULTS_CSV)

$(BENCHMARKS_RESULTS_CSV): $(BENCHMARKS_JAVASSEMBLER_WAT) $(BENCHMARKS_CPP_JS) $(BENCHMARKS_CPP_WAT)
	mkdir -p $(BENCHMARKS_RESULTS_DIR)
	$(NODE) scripts/run_benchmarks.js

.PHONY: graphs
graphs: $(BENCHMARKS_RESULTS_CSV)
	$(PYTHON) scripts/plot_graphs.py

.PHONY: clean
clean:
	$(GRADLE) clean
	rm -rf sample_programs_compiled
	rm -rf benchmarking_results
