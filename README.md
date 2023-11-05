# leaky_bucket_rate_limiting
This project is an example implementation of Leaky Bucket algorithm
Unit tests are yet to be added.
This is just an example for the demo purpose, not to be used in the practical scenario.
The main thread pushes the requests in to the bucket. If the queue is full, the requests are not accepted anymore.
The other thread processes requests after each leak interval is passed. then this thread again sleeps for the leak interval.
