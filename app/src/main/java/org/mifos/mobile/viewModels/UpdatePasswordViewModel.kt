package org.mifos.mobile.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import org.mifos.mobile.repositories.ClientRepository
import org.mifos.mobile.repositories.UserAuthRepository
import org.mifos.mobile.utils.RegistrationUiState
import javax.inject.Inject

@HiltViewModel
class UpdatePasswordViewModel @Inject constructor(
    private val userAuthRepositoryImp: UserAuthRepository,
    private val clientRepositoryImp: ClientRepository
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val _updatePasswordUiState = MutableLiveData<RegistrationUiState>()
    val updatePasswordUiState: LiveData<RegistrationUiState> get() = _updatePasswordUiState
    fun isInputFieldEmpty(fieldText: String): Boolean {
        return fieldText.isEmpty()
    }

    fun isInputLengthInadequate(fieldText: String): Boolean {
        return fieldText.length < 6
    }

    fun validatePasswordMatch(newPassword: String, confirmPassword: String): Boolean {
        return newPassword == confirmPassword
    }

    fun updateAccountPassword(newPassword: String, confirmPassword: String) {
        _updatePasswordUiState.value = RegistrationUiState.Loading
        userAuthRepositoryImp.updateAccountPassword(newPassword, confirmPassword)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribeWith(object : DisposableObserver<ResponseBody?>() {
                override fun onNext(responseBody: ResponseBody) {
                    _updatePasswordUiState.value = RegistrationUiState.Success
                    clientRepositoryImp.updateAuthenticationToken(newPassword)
                }

                override fun onError(e: Throwable) {
                    _updatePasswordUiState.value = RegistrationUiState.Error(e)
                }

                override fun onComplete() {}
            })?.let { compositeDisposable.add(it) }
    }


    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}