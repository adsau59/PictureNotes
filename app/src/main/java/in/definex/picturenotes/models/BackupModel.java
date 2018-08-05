package in.definex.picturenotes.models;

/**
 * Created by adam_ on 04-12-2016.
 */

public class BackupModel{
    public String id;
    public String name;
    public String dateModified;

    public BackupModel(String id, String name, String dateModified) {
        this.id = id;
        this.name = name;
        this.dateModified = dateModified;
    }
}