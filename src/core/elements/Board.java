package core.elements;

import java.util.List;

public class Board {
    private Integer width;
    private Integer height;
    private List<BoardElement> boardElements;
    private List<Field> fields;

    public Board(
            Integer width,
            Integer height,
            List<BoardElement> boardElements,
            List<Field> fields
    ) {
        this.width = width;
        this.height = height;
        this.boardElements = boardElements;
        this.fields = fields;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public List<BoardElement> getBoardElements() {
        return boardElements;
    }

    public void setBoardElements(List<BoardElement> boardElements) {
        this.boardElements = boardElements;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }
}
