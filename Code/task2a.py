import numpy as np

def grouping_use_svd_on_matrix(matrix, actors, num_groups):
    U, S, V = np.linalg.svd(matrix, full_matrices=False)
    create_n_groupings(V, actors, num_groups)

def create_n_groupings(matrix, labels, num_groups):
    groupings = {}
    for i in range(num_groups):
        groupings[i] = []

    for i, label in enumerate(labels):
        arr = []
        for j in range(num_groups):
            arr.append(matrix[i][j])
        index = arr.index(max(arr))
        groupings[index].append(label)

    for i in range(len(groupings)):
        print("Group",i+1," : ", groupings[i])
