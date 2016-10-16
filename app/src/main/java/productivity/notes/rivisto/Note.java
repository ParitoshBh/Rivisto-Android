package productivity.notes.rivisto;

class Note {
    private String title;
    private String content;
    private String label;
    private Long time;

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
}
