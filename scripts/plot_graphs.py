import numpy as np
import matplotlib.pyplot as plt
import pandas as pd


COLOR1 = '#68c8c6'
COLOR2 = '#ff6166'
COLOR3 = '#fcdf50'
COLOR4 = '#252a36'
COLOR4 = '#000a39'

COLOR1 = '#3b8ea5'
COLOR2 = '#f5ee9e'
COLOR3 = '#f49e4c'
COLOR4 = '#ab3428'


data_csv = pd.read_csv('benchmarking_results/benchmarking_results.csv')
sum_of_squares_data = data_csv[data_csv['Benchmark'] == 'Sum of squares']
recursion_data = data_csv[data_csv['Benchmark'] == 'Recursion']
linked_list_data = data_csv[data_csv['Benchmark'] == 'Linked list traversal']
arrays_data = data_csv[data_csv['Benchmark'] == 'Array traversal']


def plot_graph(data, x_axis_label, group_labels, title, pdf_location):
    javassembler_data = data[data['Environment'] == 'JavAssembler']
    javascript_data = data[data['Environment'] == 'JavaScript']
    cpp_data = data[data['Environment'] == 'C++']
    javassembler_nobounds_data = data[data['Environment'] == 'JavAssembler (no bounds checking)']
    javassembler_means = javassembler_data['Mean (ms)']
    javassembler_stds = javassembler_data['Standard Deviation (ms)']
    javascript_means = javascript_data['Mean (ms)']
    javascript_stds = javascript_data['Standard Deviation (ms)']
    cpp_means = cpp_data['Mean (ms)']
    cpp_stds = cpp_data['Standard Deviation (ms)']
    if len(javassembler_nobounds_data.index):
        javassembler_nobounds_means = javassembler_nobounds_data['Mean (ms)']
        javassembler_nobounds_stds = javassembler_nobounds_data['Standard Deviation (ms)']

    fig, ax = plt.subplots(figsize=(5,3.5))
    x = np.arange(len(group_labels))
    width = 0.25 # bar width
    if len(javassembler_nobounds_data.index):
        rects1 = ax.bar(x - 1.5*width, javassembler_means, width, yerr=javassembler_stds, capsize=5, color=COLOR1, label='Java (with checks)')
        rects4 = ax.bar(x - 0.5*width, javassembler_nobounds_means, width, yerr=javassembler_nobounds_stds, capsize=5, color=COLOR2, label='Java (without checks)')
        rects2 = ax.bar(x + 0.5*width, javascript_means, width, yerr=javascript_stds, capsize=5, color=COLOR3, label='JavaScript')
        rects3 = ax.bar(x + 1.5*width, cpp_means, width, yerr=cpp_stds, capsize=5, color=COLOR4, label='C++')
    else:
        rects1 = ax.bar(x - width, javassembler_means, width, yerr=javassembler_stds, capsize=5, color=COLOR1, label='JavAssembler')
        rects2 = ax.bar(x, javascript_means, width, yerr=javascript_stds, capsize=5, color=COLOR3, label='JavaScript')
        rects3 = ax.bar(x + width, cpp_means, width, yerr=cpp_stds, capsize=5, color=COLOR4, label='C++')

    ax.set_ylabel('Mean Time (ms)')
    ax.set_xlabel(x_axis_label)
    ax.set_title(title)
    ax.set_xticks(x)
    ax.set_xticklabels(group_labels)
    ax.legend()
    fig.tight_layout()

    fig.savefig(pdf_location, bbox_inches='tight')
    print('Created ' + pdf_location)


plot_graph(
    sum_of_squares_data,
    'n',
    ['1000', '10000', '50000'],
    'Sum of Squares Benchmark',
    'benchmarking_results/sum_of_squares_benchmark.pdf'
)

plot_graph(
    recursion_data,
    'Depth',
    ['100', '1000', '5000'],
    'Recursion Benchmark',
    'benchmarking_results/recursion_benchmark.pdf'
)

plot_graph(
    linked_list_data,
    'List Length',
    ['1000', '5000', '20000'],
    'Linked-List Traversal Benchmark',
    'benchmarking_results/linked_list_traversal_benchmark.pdf'
)

plot_graph(
    arrays_data,
    'Array Length',
    ['1000', '5000', '20000'],
    'Array Traversal Benchmark',
    'benchmarking_results/array_traversal_benchmark.pdf'
)
