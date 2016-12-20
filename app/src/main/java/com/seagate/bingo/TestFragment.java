package com.seagate.bingo;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class TestFragment extends Fragment implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {


    private ArrayList<Question> questions;
    private TextView questionBody;
    private Button submit;
    private boolean correctAnswer;
    private RadioGroup radioGroup;
    private int currentQuestionIndex=0;
    private Question question;
    private ProgressDialog progressDialog;

    public TestFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog=new ProgressDialog(getContext());
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading Questions");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_test, container, false);
        submit= (Button) view.findViewById(R.id.submit);
        submit.setEnabled(false);
        radioGroup= (RadioGroup) view.findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(this);
        questionBody= (TextView) view.findViewById(R.id.questionBody);
        hideViewElements(view);
        initFirebase(view);
        submit.setOnClickListener(this);

        return view;
    }

    private void hideViewElements(View view){
        progressDialog.show();
        (view.findViewById(R.id.questionBodyCard)).setVisibility(View.GONE);
        view.findViewById(R.id.cardView2).setVisibility(View.GONE);
        submit.setVisibility(View.GONE);
    }


    private void showViewElements(View view){
        progressDialog.dismiss();
        (view.findViewById(R.id.questionBodyCard)).setVisibility(View.VISIBLE);
        view.findViewById(R.id.cardView2).setVisibility(View.VISIBLE);
        submit.setVisibility(View.VISIBLE);
    }

    private void initFirebase(final View view) {
        questions=new ArrayList<>();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference();
        reference.child("Questions").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot question:dataSnapshot.getChildren()){
                    questions.add(question.getValue(Question.class));

                }
                showViewElements(view);
                displayNextQuestion();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void displayNextQuestion() {
        radioGroup.clearCheck();
        submit.setEnabled(false);
        question=questions.get(currentQuestionIndex);
        questionBody.setText(question.getQuestionBody());
        ((RadioButton)radioGroup.getChildAt(0)).setText(question.getChoice1());
        ((RadioButton)radioGroup.getChildAt(2)).setText(question.getChoice2());
        ((RadioButton)radioGroup.getChildAt(4)).setText(question.getChoice3());
        ((RadioButton)radioGroup.getChildAt(6)).setText(question.getChoice4());
        currentQuestionIndex++;
        if (currentQuestionIndex>2){
            currentQuestionIndex=0;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.submit:
                if (correctAnswer){
                    getFragmentManager().beginTransaction().remove(this).commit();
                }else {
                    Toast.makeText(getContext(),"wrong answer",Toast.LENGTH_LONG).show();
                    displayNextQuestion();

                }

        }

    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        submit.setEnabled(true);
        switch (radioGroup.getCheckedRadioButtonId()){
            case R.id.choice1:
                correctAnswer=question.getAnswer()==1;
                break;
            case R.id.choice2:
                correctAnswer=question.getAnswer()==2;
                break;
            case R.id.choice3:
                correctAnswer=question.getAnswer()==3;
                break;
            case R.id.choice4:
                correctAnswer=question.getAnswer()==4;
                break;
        }
    }
}
