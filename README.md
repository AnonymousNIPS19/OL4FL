### OL4FL: Online learning for federated learning in heterogeneous edge-cloud collaborative systems

We propose a novel framework of "learning to learn" to achieve effective federated learning (FL) with collaborations among the Cloud and edge servers under heterogeneous resource constrains.

We develop an algorithm called, the Online Learning for FL (OL4FL), to solve the problem based on budget-limited multi-armed bandit approach. OL4FL supports both synchronous and asynchronous learning patterns, and can be used for both supervised and unsupervised learning tasks. Support Vector Machine (SVM) and K-means were considered as case studies for supervised and unsupervised learning respectively. In addition, we consider two scenarios where consumes either a fixed amount of resources or variable amounts of resources at each decision.

OL4FL was implemented in Java. We encapsulated the Java codes for both edge servers and the Cloud using docker containers, and implemented the containers in a edge-Cloud testbed system consisting of three mini personal computers as the edge servers and a workstation as the Cloud server. Edge and Cloud servers are connected using Transmission Control Protocol(TCP) sockets.

