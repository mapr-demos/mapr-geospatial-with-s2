# MapR-geospatial-with-S2

### About cells

S2 cells have a level ranging from 30 ~0.7cm² to 0 ~85,000,000km². Each cell has a long id which easy to store

The main advantage is the region coverer algorithm, give it a region and the maximum number of cells you want,
S2 will return some cells at different levels that cover the region you asked for,
remember one cell corresponds to a range lookup you’ll have to perform in your database.

More info about S2 lib you can find in [**this**](http://blog.christianperone.com/2015/08/googles-s2-geometry-on-the-sphere-cells-and-hilbert-curve/) blog post.


**Prerequisites**
* MapR Converged Data Platform 6.0 with Apache Drill or [MapR Container for Developers](https://maprdocs.mapr.com/home/MapRContainerDevelopers/MapRContainerDevelopersOverview.html).
* JDK 8
* Maven 3.x

## Setting up MapR Container For Developers

MapR Container For Developers is a docker image that enables you to quickly deploy a MapR environment on your developer machine.

Installation, Setup and further information can be found [**here**](https://maprdocs.mapr.com/home/MapRContainerDevelopers/MapRContainerDevelopersOverview.html).

## Creating JSON Table in MapR 

* Create Airports table
```
$ maprcli table create -path /apps/airports -tabletype json
$ maprcli table cf edit -path /apps/airports -cfname default -readperm p -writeperm p
```

* Create Index for a airports table
```
maprcli table index add -path /apps/airports -index cellID -indexedfields cellId
```

* Create States table

```
$ maprcli table create -path /apps/states -tabletype json
$ maprcli table cf edit -path /apps/states -cfname default -readperm p -writeperm p
```
