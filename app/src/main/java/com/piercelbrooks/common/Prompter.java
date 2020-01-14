
// Author: Pierce Brooks

package com.piercelbrooks.common;

public abstract class Prompter implements PromptListener
{
    private static final String TAG = "PLB-Prompter";

    private Prompt prompt;
    private Runnable positive;
    private Runnable negative;
    private Runnable netural;
}
