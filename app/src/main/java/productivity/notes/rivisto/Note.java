package productivity.notes.rivisto;

public class Note {
    private String title;
    private String content;
    private String label;
    private Long time;
    private String noteKey;

    public Note() {
    }

    public Note(String title, String content, String label, Long time) {
        this.title = title;
        this.content = content;
        this.label = label;
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getLabel() {
        return label;
    }

    public Long getTime() {
        return time;
    }

    public void setNoteKey(String noteKey){
        this.noteKey = noteKey;
    }

    public String getNoteKey(){
        return noteKey;
    }
}
