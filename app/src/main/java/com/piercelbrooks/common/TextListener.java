
// Author: Pierce Brooks

package com.piercelbrooks.common;

import androidx.annotation.NonNull;

public interface TextListener
{
    public void onChange(@NonNull Text text);
    public void onDelete(@NonNull Text text, boolean action);
}
