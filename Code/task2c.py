from tensorly import decomposition
from tensorly import tensor
from task2a import create_n_groupings


def grouping_use_cp_on_actor_tensor(actor_movie_year_tensor, amy_info):
    # print(actor_movie_year_tensor)
    U = actor_movie_year_tensor
    T = tensor(U.reshape((U.shape[0], U.shape[1], U.shape[2])))
    # Compute rank-5 CP decomposition of Tensor with ALS

    P = decomposition.parafac(T, 5, init="random")

    # Result is a decomposed tensor stored as a Kruskal operator in P

    #fit: float
    #   Fit of the factorization compared to Tensor

    #itr : int
    #   Number of iterations that were needed until convergence

    #exectimes : ndarray of floats
     #   Time needed for each single iteration

    X = P[0].asnumpy();
    Y = P[1].asnumpy();
    Z = P[2].asnumpy();
    print("Top 5 latent sementics for Actor :")
    print(X)
    print("Top 5 latent sementics for Movie :")
    print(Y)
    print("Top 5 latent sementics for Year :")
    print(Z)
    print("Actor Groupings:")
    create_n_groupings(X, amy_info[0], 5)
    print("Movie Groupings:")
    create_n_groupings(Y, list(amy_info[2].keys()), 5)
    print("Year Groupings:")
    create_n_groupings(Z, amy_info[4], 5)

def grouping_use_cp_on_tag_tensor(tag_movie_rating, tmr_info):

    U = tag_movie_rating
    # T = vec_to_tensor(U, (U.shape[0], U.shape[1], U.shape[2]))
    T = tensor(U.reshape((U.shape[0], U.shape[1], U.shape[2])))
    # Compute rank-5 CP decomposition of Tensor with ALS and for Hosvd method replace init as nvecs
    P = decomposition.parafac(T, 5, init="random")

    # Result is a decomposed tensor stored as a Kruskal operator in P

    # fit: float
    #   Fit of the factorization compared to Tensor

    # itr : int
    #   Number of iterations that were needed until convergence

    # exectimes : ndarray of floats
    #   Time needed for each single iteration

    X = P[0].asnumpy();
    Y = P[1].asnumpy();
    Z = P[2].asnumpy();

    print("Top 5 latent sementics for Tag :")
    print(X)
    print("Top 5 latent sementics for Movie :")
    print(Y)
    print("Top 5 latent sementics for Rating :")
    print(Z)
    print("Tag Groups")
    create_n_groupings(X, tmr_info[0], 5)
    print("Movie Groups")
    create_n_groupings(Y, tmr_info[2], 5)
    print(" Rating Groups:")
    create_n_groupings(Z, tmr_info[4], 5)
