Knights of Columbus EDW 2473 2013 Pool Membership Application
=============================================================
This is a project for the 2013 Pool Membership form for the Arlington Knights of Colubmus 2013 pool
membership.  The application was built using Spring MVC, Spring JPA, Maven, Google App Engine, JQuery, and
twitter-bootstrap (amongst others).  A more thorough introduction will be forthcoming.

Usage
-----
This is a maven project, and it utilizes the _appengine-maven-plugin_ to produce appengine-compatible code,
including the _maven-datanucleus-plugin_ to process JPA annotations into DataStore entities.  As a result,
building can be very slow.  If you have the maven eclipse plugin (_m2e_) and the _m2e-wtp_ plugins, you
__should__ be able to run 'mvn eclipse:eclipse' to get a valid eclipse project.  However, I never had any
luck running within eclipse, and frankly never cared as I would run from the command line using 'mvn 
appengine:devserver'.  For a full list of appengine commands, run 'mvn help:describe -Dplugin=appengine'