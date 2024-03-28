import matplotlib.pyplot as plt
import numpy as np
import pandas as pd

data = pd.read_csv('graph.dat', sep=' ', header=None, names=['X', 'Y'])

# Extract unique nodes
unique_nodes = data.drop_duplicates()

# Print coordinates and annotate nodes

plt.scatter(unique_nodes['X'], unique_nodes['Y'])
plt.xlabel('X')
plt.ylabel('Y')
plt.title('P2P Network Topology')
plt.grid(True)

# Draw connections between consecutive points
for i in range(0, len(data) - 1, 2):
    plt.plot([data.iloc[i]['X'], data.iloc[i + 1]['X']], [data.iloc[i]['Y'], data.iloc[i + 1]['Y']], 'k-')


plt.show()