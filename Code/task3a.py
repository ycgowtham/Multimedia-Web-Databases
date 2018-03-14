from numpy import zeros
from numpy import nan_to_num
from numpy import divide
from numpy import seterr
from numpy import dot
from numpy import count_nonzero
from numpy import any
from numpy import fill_diagonal


def random_walk_restarts_on_matrix(matrix, objects, object_to_index, seeds):
    """
    Random walk with restarts
    :param matrix: object-object matrix (transition matrix)
    :param object_to_index: a dictionary to convert (object -> index in the matrix)
    :param seeds: seeds set provided by user
    :return u: Personalized Page Rank for each objects
    """

    # init v = 0, except for seeds = 1
    seeds_len = len(seeds)
    if seeds_len == 0:
        print('at least one seed required')
        return []

    for seed in seeds:
        if seed not in object_to_index:
            print('invalid actor id:', seed)
            return []

    v = zeros(len(object_to_index))
    for seed in seeds:
        v[object_to_index[seed]] = 1 / seeds_len

    # normalize matrix, column sum up to 1
    seterr(divide='ignore', invalid='ignore')

    fill_diagonal(matrix, 0)
    matrix = nan_to_num(divide(matrix, count_nonzero(matrix, axis=0)))
    matrix[:, ~any(matrix, axis=0)] = 1 / len(object_to_index)

    # u = v
    u = v.copy()

    # decide a c, application related, hard code 0.2 first.
    c = 0.2

    delta_u = u
    # while u is not converged (L1 distance here, we can change to L2, L3, L-inf based on application)
    while max(delta_u) > 1e-06:
        # u = (1 - c) * matrix * u + c * v
        u_ = (1 - c) * dot(matrix, u) + c * v
        delta_u = u - u_
        u = u_

    result = {}
    for i, v in enumerate(objects):
        result[v] = u[i]

    ret = []
    for i in (sorted(result.items(), key=lambda x: x[1], reverse=True)):
        if i[0] in seeds:
            continue

        ret.append(i)

    return ret
