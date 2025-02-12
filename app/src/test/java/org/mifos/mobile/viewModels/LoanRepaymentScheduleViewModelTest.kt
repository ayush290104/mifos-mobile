package org.mifos.mobile.viewModels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import io.reactivex.Observable
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mifos.mobile.models.accounts.loan.LoanWithAssociations
import org.mifos.mobile.repositories.LoanRepositoryImp
import org.mifos.mobile.util.RxSchedulersOverrideRule
import org.mifos.mobile.utils.Constants
import org.mifos.mobile.utils.LoanUiState
import org.mockito.Mock
import org.mifos.mobile.R
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class LoanRepaymentScheduleViewModelTest {

    @JvmField
    @Rule
    val mOverrideSchedulersRule = RxSchedulersOverrideRule()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    lateinit var loanRepositoryImp: LoanRepositoryImp

    @Mock
    lateinit var loanUiStateObserver: Observer<LoanUiState>

    private lateinit var viewModel: LoanRepaymentScheduleViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        viewModel = LoanRepaymentScheduleViewModel(loanRepositoryImp)
        viewModel.loanUiState.observeForever(loanUiStateObserver)
    }

    @Test
    fun testLoanLoanWithAssociations_Successful() {
        val response = mock(LoanWithAssociations::class.java)
        `when`(
            loanRepositoryImp.getLoanWithAssociations(
                Constants.REPAYMENT_SCHEDULE,
                1
            )
        ).thenReturn(Observable.just(response))

        viewModel.loanLoanWithAssociations(1)
        verify(loanUiStateObserver).onChanged(LoanUiState.Loading)
        if (response.repaymentSchedule?.periods?.isNotEmpty() == true) {
            verify(loanUiStateObserver).onChanged(LoanUiState.ShowLoan(response))
            verifyNoMoreInteractions(loanUiStateObserver)
        } else {
            verify(loanUiStateObserver).onChanged(LoanUiState.ShowEmpty(response))
            verifyNoMoreInteractions(loanUiStateObserver)
        }
    }

    @Test
    fun testLoanLoanWithAssociations_Unsuccessful() {
        val error = RuntimeException("Error Response")
        `when`(
            loanRepositoryImp.getLoanWithAssociations(
                Constants.REPAYMENT_SCHEDULE,
                1
            )
        ).thenReturn(
            Observable.error(error)
        )
        viewModel.loanLoanWithAssociations(1)
        verify(loanUiStateObserver).onChanged(LoanUiState.Loading)
        verify(loanUiStateObserver).onChanged(LoanUiState.ShowError(R.string.repayment_schedule))
    }

    @After
    fun tearDown() {
        viewModel.loanUiState.removeObserver(loanUiStateObserver)
    }


}