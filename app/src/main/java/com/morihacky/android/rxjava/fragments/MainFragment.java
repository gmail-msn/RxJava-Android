package com.morihacky.android.rxjava.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.OnClick;

import com.morihacky.android.rxjava.R;
import com.morihacky.android.rxjava.rxbus.RxBusDemoFragment;

public class MainFragment
        extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.inject(this, layout);
        return layout;
    }

    @OnClick(R.id.btn_demo_schedulers)
    public void demoConcurrencyWithSchedulers() {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(ConcurrencyWithSchedulersDemoFragment.class.getName())
                .replace(android.R.id.content,
                        new ConcurrencyWithSchedulersDemoFragment(),
                        ConcurrencyWithSchedulersDemoFragment.class.getName())
                .commit();
    }

    @OnClick(R.id.btn_demo_custom)
    public void demoCustom() {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(CreateModeFragment.class.getName())
                .replace(android.R.id.content,
                        new CreateModeFragment(), CreateModeFragment.class.getName())
                .commit();
    }

    @OnClick(R.id.btn_error_handler)
    public void errorHandler() {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(ErrorHandlingOperatorsFragment.class.getName())
                .replace(android.R.id.content,
                        new ErrorHandlingOperatorsFragment(), ErrorHandlingOperatorsFragment.class.getName())
                .commit();
    }

    @OnClick(R.id.btn_demo_buffer)
    public void demoBuffer() {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(BufferDemoFragment.class.toString())
                .replace(android.R.id.content,
                        new BufferDemoFragment(),
                        BufferDemoFragment.class.toString())
                .commit();
    }

    @OnClick(R.id.btn_filtering_observables)
    public void filteringObservable() {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(FilteringObservablesFragment.class.toString())
                .replace(android.R.id.content,
                        new FilteringObservablesFragment(),
                        FilteringObservablesFragment.class.toString())
                .commit();
    }

    @OnClick(R.id.btn_transforming_observables)
    public void transformingOperators() {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(TransformingFragment.class.toString())
                .replace(android.R.id.content,
                        new TransformingFragment(),
                        TransformingFragment.class.toString())
                .commit();
    }

    @OnClick(R.id.btn_demo_debounce)
    public void demoThrottling() {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(DebounceSearchEmitterFragment.class.toString())
                .replace(android.R.id.content,
                        new DebounceSearchEmitterFragment(),
                        DebounceSearchEmitterFragment.class.toString())
                .commit();
    }

    @OnClick(R.id.btn_combining_observables)
    public void combiningObservables() {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(CombiningObservablesFragment.class.toString())
                .replace(android.R.id.content,
                        new CombiningObservablesFragment(),
                        CombiningObservablesFragment.class.toString())
                .commit();
    }

    @OnClick(R.id.btn_demo_retrofit)
    public void demoRetrofitCalls() {
        getActivity().getSupportFragmentManager()
                .beginTransaction().addToBackStack(RetrofitFragment.class.toString())
                //.replace(android.R.id.content, new RetrofitAsyncTaskDeathFragment(), RetrofitAsyncTaskDeathFragment.class.toString())
                .replace(android.R.id.content,
                        new RetrofitFragment(),
                        RetrofitFragment.class.toString()).commit();
    }

    @OnClick(R.id.btn_demo_double_binding_textview)
    public void demoDoubleBindingWithPublishSubject() {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(DoubleBindingTextViewFragment.class.toString())
                .replace(android.R.id.content,
                        new DoubleBindingTextViewFragment(),
                        DoubleBindingTextViewFragment.class.toString())
                .commit();
    }

    /*@OnClick(R.id.btn_demo_polling)
    public void demoPolling() {
        getActivity().getSupportFragmentManager()
              .beginTransaction()
              .addToBackStack(PollingFragment.class.toString())
              .replace(android.R.id.content, new PollingFragment(), PollingFragment.class.toString())
              .commit();
    }*/

    @OnClick(R.id.btn_demo_rxbus)
    public void demoRxBus() {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(RxBusDemoFragment.class.toString())
                .replace(android.R.id.content,
                        new RxBusDemoFragment(),
                        RxBusDemoFragment.class.toString())
                .commit();
    }

    //@OnClick(R.id.btn_demo_subject_timeout)
    public void demoTimeout() {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(TimeoutDemoFragment.class.toString())
                .replace(android.R.id.content,
                        new TimeoutDemoFragment(),
                        TimeoutDemoFragment.class.toString())
                .commit();
    }

    @OnClick(R.id.btn_demo_form_validation_combinel)
    public void formValidation() {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(FormValidationCombineLatestFragment.class.toString())
                .replace(android.R.id.content,
                        new FormValidationCombineLatestFragment(),
                        FormValidationCombineLatestFragment.class.toString())
                .commit();
    }

    @OnClick(R.id.btn_demo_pseudo_cache)
    public void pseudoCacheDemo() {
        getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(
                PseudoCacheMergeFragment.class.toString())
                //.replace(android.R.id.content, new PseudoCacheConcatFragment(), PseudoCacheConcatFragment.class.toString())
                .replace(android.R.id.content,
                        new PseudoCacheMergeFragment(),
                        PseudoCacheMergeFragment.class.toString()).commit();
    }

    @OnClick(R.id.btn_demo_timing)
    public void demoTimerIntervalDelays() {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(TimingDemoFragment.class.toString())
                .replace(android.R.id.content,
                        new TimingDemoFragment(),
                        TimingDemoFragment.class.toString())
                .commit();
    }

    @OnClick(R.id.btn_demo_exponential_backoff)
    public void demoExponentialBackoff() {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(ExponentialBackoffFragment.class.toString())
                .replace(android.R.id.content,
                        new ExponentialBackoffFragment(),
                        ExponentialBackoffFragment.class.toString())
                .commit();
    }

    @OnClick(R.id.btn_demo_rotation_persist)
    public void demoRotationPersist() {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(RotationPersistFragment.class.toString())
                .replace(android.R.id.content,
                        new RotationPersistFragment(),
                        RotationPersistFragment.class.toString())
                .commit();
    }

    @OnClick(R.id.btn_demo_conditional_boolean_operators)
    public void conditionalBooleanOperators() {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(ConditionalBooleanOperatorsFragment.class.toString())
                .replace(android.R.id.content,
                        new ConditionalBooleanOperatorsFragment(),
                        ConditionalBooleanOperatorsFragment.class.toString())
                .commit();
    }

    @OnClick(R.id.btn_demo_blocking_observable_operators)
    public void blockingObservableOperators() {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(BlockingObservableFragment.class.toString())
                .replace(android.R.id.content,
                        new BlockingObservableFragment(),
                        BlockingObservableFragment.class.toString())
                .commit();
    }

    @OnClick(R.id.btn_demo_mathematical_aggregate_operators)
    public void mathematicalAggregateOperators() {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(MathematicalAggregateFragment.class.toString())
                .replace(android.R.id.content,
                        new MathematicalAggregateFragment(),
                        MathematicalAggregateFragment.class.toString())
                .commit();
    }

    @OnClick(R.id.btn_demo_string_observable_operators)
    public void stringObservableOperators() {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(StringObservablesFragment.class.toString())
                .replace(android.R.id.content,
                        new StringObservablesFragment(),
                        StringObservablesFragment.class.toString())
                .commit();
    }

    @OnClick(R.id.btn_demo_side_effect_observable)
    public void sideEffectMethods() {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(SideEffectFragment.class.toString())
                .replace(android.R.id.content,
                        new SideEffectFragment(),
                        SideEffectFragment.class.toString())
                .commit();
    }
}
