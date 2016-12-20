package com.seagate.bingo;

/**
 * Created by Muhammad Workstation on 14/12/2016.
 */

public class Question {
    private String questionBody,choice1,choice2,choice3,choice4;
    private int answer;

    public Question() {
    }

    public Question(String questionBody, String choice1, String choice2, String choice3, String choice4, int answer){
        this.questionBody=questionBody;
        this.choice1=choice1;
        this.choice2=choice2;
        this.choice3=choice3;
        this.choice4=choice4;
        this.answer=answer;
    }

    public String getQuestionBody() {
        return questionBody;
    }

    public void setQuestionBody(String questionBody) {
        this.questionBody = questionBody;
    }

    public String getChoice1() {
        return choice1;
    }

    public void setChoice1(String choice1) {
        this.choice1 = choice1;
    }

    public String getChoice2() {
        return choice2;
    }

    public void setChoice2(String choice2) {
        this.choice2 = choice2;
    }

    public String getChoice3() {
        return choice3;
    }

    public void setChoice3(String choice3) {
        this.choice3 = choice3;
    }

    public String getChoice4() {
        return choice4;
    }

    public void setChoice4(String choice4) {
        this.choice4 = choice4;
    }


    public int getAnswer() {
        return answer;
    }

    public void setAnswer(int answer) {
        this.answer = answer;
    }
}
