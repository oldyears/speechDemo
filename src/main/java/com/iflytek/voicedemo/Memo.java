// Memo.java
package com.iflytek.voicedemo;

import java.io.Serializable;




public class Memo implements Serializable {

    private String title;
    private String content;

    public Memo(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

}
