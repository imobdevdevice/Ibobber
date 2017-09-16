package com.reelsonar.ibobber.model;

/**
 * Created by rujul on 16/9/17.
 */

public class Intro {

    private String content, numberTxt;
    private int selectedPos, image;
    private Boolean isTitleVisible;

    public Intro(String content, String numberTxt, int img, int selectedPos) {
        this.content = content;
        this.numberTxt = numberTxt;
        this.selectedPos = selectedPos;
        this.image = img;
        this.isTitleVisible = numberTxt.length() > 0;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getNumberTxt() {
        return numberTxt;
    }

    public void setNumberTxt(String numberTxt) {
        this.numberTxt = numberTxt;
    }

    public int getSelectedPos() {
        return selectedPos;
    }

    public void setSelectedPos(int selectedPos) {
        this.selectedPos = selectedPos;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public boolean isTitleVisible() {
        return isTitleVisible;
    }

    public void setTitleVisible(Boolean titleVisible) {
        isTitleVisible = titleVisible;
    }
}
