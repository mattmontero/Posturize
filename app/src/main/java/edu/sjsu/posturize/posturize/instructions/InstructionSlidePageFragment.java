package edu.sjsu.posturize.posturize.instructions;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import edu.sjsu.posturize.posturize.R;

/**
 * Created by markbragg on 11/29/17.
 */

@SuppressLint("ValidFragment")
public class InstructionSlidePageFragment extends Fragment {

    private String instruction;
    private TextView textViewInstruction;

    public InstructionSlidePageFragment(String instruction) {
        this.instruction = instruction;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_instruction_slide_page, container, false);
        textViewInstruction = ((TextView)rootView.findViewById(R.id.instruction_slide_text_view));
        textViewInstruction.setText(instruction);
        textViewInstruction.setTextColor(Color.BLACK);
        return rootView;
    }
}


