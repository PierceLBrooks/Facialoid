
// Author: Pierce Brooks

package com.piercelbrooks.common;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import android.view.View;

public interface Mayor <T extends Enum<T>> extends Citizen
{

    public void onBirth();
    public void onDeath();
    public T getMayoralFamily();
    public Municipality<T> getMunicipality();
    public @LayoutRes int getLayout();
    public void createView(@NonNull View view);
}
