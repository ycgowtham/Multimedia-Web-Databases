import math
import numpy
from random import uniform

class HashFamily:

    def __init__(self, n, d=500):
        self.t = math.log(n)
        self.t = int(self.t)
        self.A_matrix = numpy.zeros((self.t, d))      #t * d matrix
        self.U = n
        # A[i][j] = Normal(0, 1)
        for i in range(len(self.A_matrix)):
            for j in range(len(self.A_matrix[i])):
                self.A_matrix[i][j] = numpy.random.normal(0, 1) * 1/math.sqrt(self.t)
        self.w = 0.5                                # w=1/2?
        # U grids of balls Su = [0, 4w].
        self.Su = []
        for i in range(self.U):
            self.Su.append(uniform(0, self.w*4))

    def compute_hash(self, p):
        p_dash = self.A_matrix * numpy.matrix(p).transpose()
        p_dash = p_dash.transpose().tolist()[0]
        for j in range(self.U):
            ball = [0] * self.t
            dist = 0
            for i in range(self.t):
                ball[i] = (p_dash[i]-self.Su[j])/self.w
                dist += math.pow((p_dash[i]-self.Su[j])%self.w,2)
            dist = math.sqrt(dist)
            if dist < self.w:
                return (j, tuple(ball))
        return None

if __name__ == '__main__':
    X = HashFamily(100)
    X.compute_hash([1,2,3,4])
