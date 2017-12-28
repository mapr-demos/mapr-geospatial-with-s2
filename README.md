# MapR-geospatial-with-S2

### About cells
```text
    S2 cells have a level ranging from 30 ~0.7cm² to 0 ~85,000,000km²
Each cell has a long id which easy to store

    The main advantage is the region coverer algorithm,
give it a region and the maximum number of cells you want,
S2 will return some cells at different levels that cover the region you asked for,
remember one cell corresponds to a range lookup you’ll have to perform in your database.
```
