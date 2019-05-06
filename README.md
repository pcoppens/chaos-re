# chaos-re
Chaos Reverse-engineering
git: https://github.com/pcoppens/chaos-re

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
