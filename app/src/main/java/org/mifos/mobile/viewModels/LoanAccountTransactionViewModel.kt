package org.mifos.mobile.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import org.mifos.mobile.R
import org.mifos.mobile.models.accounts.loan.LoanWithAssociations
import org.mifos.mobile.repositories.LoanRepository
import org.mifos.mobile.utils.Constants
import org.mifos.mobile.utils.LoanUiState
import javax.inject.Inject

@HiltViewModel
class LoanAccountTransactionViewModel @Inject constructor(private val loanRepositoryImp: LoanRepository) :
    ViewModel() {

    private val compositeDisposables: CompositeDisposable = CompositeDisposable()

    private val _loanUiState = MutableLiveData<LoanUiState>()
    val loanUiState: LiveData<LoanUiState> get() = _loanUiState

    fun loadLoanAccountDetails(loanId: Long?) {
        _loanUiState.value = LoanUiState.Loading
        loanRepositoryImp.getLoanWithAssociations(
            Constants.TRANSACTIONS,
            loanId,
        )?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribeOn(Schedulers.io())
            ?.subscribeWith(object : DisposableObserver<LoanWithAssociations?>() {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    _loanUiState.value =
                        LoanUiState.ShowError(R.string.loan_account_details)
                }

                override fun onNext(loanWithAssociations: LoanWithAssociations) {
                    if (loanWithAssociations.transactions != null &&
                        loanWithAssociations.transactions?.isNotEmpty() == true
                    ) {
                        _loanUiState.value = LoanUiState.ShowLoan(loanWithAssociations)
                    } else {
                        _loanUiState.value = LoanUiState.ShowEmpty(loanWithAssociations)
                    }
                }
            })?.let {
                compositeDisposables.add(
                    it,
                )
            }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposables.clear()
    }
}