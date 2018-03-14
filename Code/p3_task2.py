import math


def pdiff(tags_per_movie, relevant_movies):
    """
    Calculating pdiff of each tags based
    :param tags_per_movie: movies mapping into tag vector. {'movie1': {'tg1': 1, 'tg2': 2}, 'movie2': {'tg1': 1}}
    :param relevant_movies: relevant movies list. ['movie1', 'movie2', 'movie3']
    :return: pdiff probabilistic different result of each tag {'tg1': 11, 'tg2': 22}
    """
    diff = {}
    movies = tags_per_movie.keys()

    R = len(relevant_movies)
    M = len(movies)

    for movie in movies:
        for tag in tags_per_movie[movie]:
            if tag not in diff:
                diff[tag] = {}
                diff[tag]['r'] = 0
                diff[tag]['m'] = 0
            diff[tag]['m'] += 1
            if movie in relevant_movies:
                diff[tag]['r'] += 1

    for tag in diff:
        m = diff[tag]['m']
        r = diff[tag]['r']
        # print(diff[tag]['r'], diff[tag]['m'])
        n_offset = 0
        d_offset = 0
        fixed_correction = 0.5 # or m / M on R = 0
        if (M == R) or (m == r) or (R == r) or (r == 0) or (M - m - R + r == 0):
            d_offset = 1
            n_offset = d_offset - fixed_correction

        diff[tag] = math.log(((r + n_offset) * (M - m - R + r + n_offset)) / ((R - r + n_offset) * (m - r + n_offset)))

    return diff

def gen_prob_feedback_function(tags_per_movie):
    """
    :param tags_per_movie: movies mapping into tag vector. {'movie1': {'tg1': 1, 'tg2': 2}, 'movie2': {'tg1': 1}}
    :return: function(['movie_1', ...],['movie_4', ...])
    """
    def probabilistic_relevance_feedback(relevant_movies):
        """
        Calculating similarity between all movies and relevant movies.
        :param relevant_movies: relevant movies list. ['movie1', 'movie2', 'movie3']
        :return: relevance_feedback. {'movie1' : {'relevent movie1': 1.69, 'relevent movie2': 2.69},
                                      'movie2' : {'relevent movie1': 0.69, 'relevent movie2': 2.88}}
        """
        diff = pdiff(tags_per_movie, relevant_movies)

        relevance_feedback = {}
        for movie, tags in tags_per_movie.items():
            similarity = {}
            for query in relevant_movies:
                similarity[query] = 0
                for tag in tags:
                    if tag in tags_per_movie[query]:
                        similarity[query] += diff[tag]

            relevance_feedback[movie] = similarity

        return relevance_feedback
    return probabilistic_relevance_feedback
