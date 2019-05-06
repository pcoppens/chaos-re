# chaos-re
Chaos engineering approach by Reverse-engineering.

git: https://github.com/pcoppens/chaos-re

# Introduction
Chaos engineering leads to more resilient systems and builds confidence in a software product based on a large scale distributed architecture. 

The discipline is based on the definition of a steady-state and the assertion that it will remain stable when a fault is injected in  production.

The approach by Reverse-engineering reduce, or eliminate the intuitive approach by offer a visual representation of the system.

# Structure
/

/back-end/ {projet java}

         application/core/ {discover Tool (use Bayes theorem)}
         
         application/generator/ {Labo: generate a random System}
         
         application/input/ {Read source and build an abstraction of a System}
         
         application/model/ {Abstraction of a distributed System}
         
         application/output/ {write a representation of the System}
         
              dot/ {write a System to (Graphiz) dot file}
              
              vizceral/ {write a System to Vizceral file}
         
/dot {dot files resuult}

/vizceral {Vizceral front-end}

# Discover a system
1. Get log files or build a generated system (/back-end/generator/DistribuedSystem.main)
2. Run a process (application/ProcessForemLogs or application/ProcessLogs)
3. See representation (generate images from dot/files.dot or run Vizceral: See vizceral/README.md )
