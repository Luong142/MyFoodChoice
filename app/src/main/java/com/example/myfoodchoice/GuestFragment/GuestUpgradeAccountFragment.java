package com.example.myfoodchoice.GuestFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.myfoodchoice.R;

public class GuestUpgradeAccountFragment extends Fragment
{
    // TODO: here is our plan: make the guest account can be upgraded to user account for more accessing features.
    // TODO: this fragment should be able to upgrade by changing the account type in the realtime database.
    // TODO: 7 days trial too

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);



    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_guest_upgrade_account, container, false);
    }
}