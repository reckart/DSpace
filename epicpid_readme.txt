This Dspace version will probably work only with epic pid service (not tested what will happen if the PIDService setting are not added to config file)
In order to activate Epic PidService you have to add this lines to the dspace.cfg file, located in[dspace-install]/dspace/config/dspace.cfg), add it after line

handle.canonical.prefix = http://hdl.handle.net/ <--- add here the handle prefix for example http://hdl.handle.net/11148/

# EPIC PID Service handle configuration
handle.service.url = url with prefix
handle.service.user = username     
handle.service.pass = password
handle.service.version = epic pid version 1 or 2 


Successfully tested on localhast with demo pid service:
-creating a collection/Community
-uploading a file
