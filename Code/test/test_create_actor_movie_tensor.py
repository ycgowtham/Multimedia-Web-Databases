import sys, unittest, numpy

sys.path.append('..')
sys.path.append('.')

import tensor_builder

class TestCreateAMYTensor(unittest.TestCase):
    def test_create_AMY_Tensor(self):
        actor_movie = [{ 'movieid': '1', 'actorid': '1'},
                    { 'movieid': '1', 'actorid': '2'},
                    { 'movieid': '1', 'actorid': '5'},
                    { 'movieid': '1', 'actorid': '3'},
                    { 'movieid': '2', 'actorid': '3'},
                    { 'movieid': '2', 'actorid': '1'},
                    { 'movieid': '3', 'actorid': '4'}]
        movies = [{'movieid': '1', 'movie': 'Movie1', 'year': '2001'},
                {'movieid': '2', 'movie': 'Movie2', 'year': '2003'},
                {'movieid': '3', 'movie': 'Movie3', 'year': '2003'}]

        tensor = numpy.zeros((5, 3, 2))

        tensor[0][0][0] = 1
        tensor[0][0][1] = 0
        tensor[0][1][0] = 0
        tensor[0][1][1] = 1
        tensor[0][2][0] = 0
        tensor[0][2][1] = 0
        tensor[1][0][0] = 1
        tensor[1][0][1] = 0
        tensor[1][1][0] = 0
        tensor[1][1][1] = 0
        tensor[1][2][0] = 0
        tensor[1][2][1] = 0
        tensor[2][0][0] = 1
        tensor[2][0][1] = 0
        tensor[2][1][0] = 0
        tensor[2][1][1] = 1
        tensor[2][2][0] = 0
        tensor[2][2][1] = 0
        tensor[3][0][0] = 0
        tensor[3][0][1] = 0
        tensor[3][1][0] = 0
        tensor[3][1][1] = 0
        tensor[3][2][0] = 0
        tensor[3][2][1] = 1
        tensor[4][0][0] = 1
        tensor[4][0][1] = 0
        tensor[4][1][0] = 0
        tensor[4][1][1] = 0
        tensor[4][2][0] = 0
        tensor[4][2][1] = 0

        actual, meta = tensor_builder.build_actor_movie_year_tensor(actor_movie, movies)
        self.assertEquals(len(actual), 5)
        self.assertEquals(len(actual[0]), 3)
        self.assertEquals(len(actual[0][0]), 2)

        for i in range(5):
            for j in range(3):
                for k in range(2):
                    try:
                        self.assertEquals(actual[i][j][k], tensor[i][j][k])
                    except Exception as e:
                        print("Error at indices: {0}, {1}, {2}".format(i,j,k))
                        raise e

        actor_dict = meta[1]
        movie_dict = meta[3]
        year_dict = meta[5]
        self.assertEquals(actor_dict['1'], 0)
        self.assertEquals(actor_dict['2'], 1)
        self.assertEquals(actor_dict['3'], 2)
        self.assertEquals(actor_dict['4'], 3)
        self.assertEquals(actor_dict['5'], 4)
        self.assertEquals(movie_dict['1'], 0)
        self.assertEquals(movie_dict['2'], 1)
        self.assertEquals(movie_dict['3'], 2)
        self.assertEquals(year_dict['2001'], 0)
        self.assertEquals(year_dict['2003'], 1)

if __name__ == '__main__':
    unittest.main()
