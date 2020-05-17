package com.bordercloud.sparql;

public enum MimeType {

    rdf("rdf",  "application/rdf+xml"),
    text("nt", "text/plain"),
    csv( "csv", "text/csv"),
    tsv("tsv", "text/tab-separated-values"),
    ttl( "ttl", "text/turtle"),
    txt( "txt", "text/plain"),
    html( "html", "text/html"),
    xml("srx",  "application/sparql-results+xml"),
    json("srj", "application/sparql-results+json");

    private String extension = "";
    private String mimetype = "";
    //Constructeur
    MimeType(String extension, String mimetype){
        this.extension = extension;
        this.mimetype = mimetype;
    }

    public static MimeType Search(String mimetypeSTR){
        for (MimeType mimeType : MimeType.values()) {
            if(mimeType.getMimetype().equals(mimetypeSTR)){
                return mimeType;
            }
        }
        return null;
    }

    public String getExtension() {
        return extension;
    }

    public String getMimetype() {
        return mimetype;
    }

    public String toString(){
        return mimetype;
    }
}