package tech.gralerfics.gralculator.fragments.calculator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.HashMap;

import tech.gralerfics.gralculator.R;
import tech.gralerfics.gralculator.databinding.FragmentCalculatorBinding;

public class CalculatorFragment extends Fragment {

    private FragmentCalculatorBinding binding;

    private HashMap<Integer, String> buttonEvents;

    private void bindButtonEvents(View view) {
        PixelScreenView pixelScreenView = view.findViewById(R.id.screen);

        for (int btnId : buttonEvents.keySet()) {
            Button btn = view.findViewById(btnId);
            String event = buttonEvents.get(btnId);
            if (event != null) {
                if (event.charAt(0) == '$') {
                    // Events
                    if (event.contains("execute")) {
                        btn.setOnClickListener(_view -> pixelScreenView.eventExecute());
                    } else if (event.contains("clearall")) {
                        btn.setOnClickListener(_view -> pixelScreenView.eventClearall());
                    } else if (event.contains("delete")) {
                        btn.setOnClickListener(_view -> pixelScreenView.eventDelete());
                    } else if (event.contains("left")) {
                        btn.setOnClickListener(_view -> pixelScreenView.eventLeft());
                    } else if (event.contains("right")) {
                        btn.setOnClickListener(_view -> pixelScreenView.eventRight());
                    }
                } else {
                    // Raw string
                    btn.setOnClickListener(_view -> pixelScreenView.eventInput(event));
                }
            }
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCalculatorBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        buttonEvents = new HashMap<>();

        // Numpad Binding
        buttonEvents.put(R.id.button_numdot, ".");
        buttonEvents.put(R.id.button_numcomma, ",");
        buttonEvents.put(R.id.button_num0, "0");
        buttonEvents.put(R.id.button_num1, "1");
        buttonEvents.put(R.id.button_num2, "2");
        buttonEvents.put(R.id.button_num3, "3");
        buttonEvents.put(R.id.button_num4, "4");
        buttonEvents.put(R.id.button_num5, "5");
        buttonEvents.put(R.id.button_num6, "6");
        buttonEvents.put(R.id.button_num7, "7");
        buttonEvents.put(R.id.button_num8, "8");
        buttonEvents.put(R.id.button_num9, "9");
        buttonEvents.put(R.id.button_numadd, "+");
        buttonEvents.put(R.id.button_numsub, "-");
        buttonEvents.put(R.id.button_nummul, "*");
        buttonEvents.put(R.id.button_numdiv, "/");
        buttonEvents.put(R.id.button_numc, "$clearall");
        buttonEvents.put(R.id.button_numdel, "$delete");
        buttonEvents.put(R.id.button_numexe, "$execute");

        // Extpad Binding
        buttonEvents.put(R.id.button_extpwr2, "^2");
        buttonEvents.put(R.id.button_extpwr, "^");
        buttonEvents.put(R.id.button_extln, "ln(");
        buttonEvents.put(R.id.button_extsin, "sin(");
        buttonEvents.put(R.id.button_extcos, "cos(");
        buttonEvents.put(R.id.button_exttan, "tan(");
        buttonEvents.put(R.id.button_extsqrt2, "sqrt(");
        buttonEvents.put(R.id.button_extsqrtn, "sqrt(n,");
        buttonEvents.put(R.id.button_extleftbracket, "(");
        buttonEvents.put(R.id.button_extrightbracket, ")");
        buttonEvents.put(R.id.button_extans, "Ans");
        buttonEvents.put(R.id.button_extfunc, "$func");

        // Arrowpad Binding
        buttonEvents.put(R.id.button_arrowup, "$up");
        buttonEvents.put(R.id.button_arrowdown, "$down");
        buttonEvents.put(R.id.button_arrowleft, "$left");
        buttonEvents.put(R.id.button_arrowright, "$right");
        buttonEvents.put(R.id.button_arrowok, "$confirm");
        buttonEvents.put(R.id.button_arrowshift, "$shift");
        buttonEvents.put(R.id.button_arrowoptn, "$operation");
        buttonEvents.put(R.id.button_arrowalpha, "$alpha");
        buttonEvents.put(R.id.button_arrowvars, "$variables");
        buttonEvents.put(R.id.button_arrowmenu, "$menu");
        buttonEvents.put(R.id.button_arrowabout, "$about");
        buttonEvents.put(R.id.button_arrowsetup, "$setup");
        buttonEvents.put(R.id.button_arrowclear, "$clear");

        // Event Binding
        bindButtonEvents(root);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}