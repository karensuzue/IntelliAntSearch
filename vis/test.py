import networkx as nx
import matplotlib.pyplot as plt
import re

def parse_log_output(log_output):
    paths = []
    # Regex to find numeric sequences inside brackets
    path_regex = re.compile(r'path=\[(.*?)\]')
    for line in log_output.split('\n'):
        match = path_regex.search(line)
        if match:
            path_str = match.group(1)
            path = [int(node) for node in path_str.split(", ") if node.isdigit()]
            paths.append(path)
    return paths

with open("log.txt", "r") as f:
    data = f.read()
    

# Parse the updated log output to get paths
paths = parse_log_output(data)

# Create a directed graph
G = nx.DiGraph()

# Add directed edges based on paths
for path in paths:
    for i in range(len(path) - 1):
        G.add_edge(path[i], path[i+1])

# Choose a layout for our graph
pos = nx.spring_layout(G, k=0.15, iterations=20)  # k: Optimal distance between nodes. Increase to spread out nodes

# Create figure with specified size and layout management
plt.figure(figsize=(15, 10), constrained_layout=True)

# Draw the directed network with arrows
nx.draw(G, pos, with_labels=True, node_size=250, node_color='skyblue', font_size=10, font_weight='bold',
        edge_color='gray', width=0.5, alpha=0.7, arrows=True, arrowsize=10, arrowstyle='-|>')

# Save the plot to a file
plt.savefig("network_graph.png", format="PNG")

# Show the plot
plt.show()
