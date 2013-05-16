In order to activate Dariah Storage you have to add this lines to the dspace.cfg file, located in[dspace-install]/dspace/config/dspace.cfg), add it after line
assetstore.dir = ${dspace.dir}/assetstore


dariah.baseurl =  baseurl without a slash at the end
dariah.idpurl = idpurl without a slash at the end
dariah.username = username
dariah.password = password

 
Probably the register method will not work with Dariah Storage


If Dariah Storage service is running on localhost, you have to modify the getDariahClient method and create the client as follow: 
StorageClient client = StorageClient.createClient(dariahAccount.getBaseUrl());

