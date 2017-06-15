# andhook
andhook is a lightweight hook framework for android, which supports both dalvik and art environment.    
It consists of three part:    
- AndHook.java    
	- optional bridge class of ArtHook and DalvikHook
- ArtHook.java    
	- ART hook implementation, based on [epic](https://github.com/tiann/epic)
- DalvikHook.java    
	- Dalvik VM hook implementation, primarily written in C++    
