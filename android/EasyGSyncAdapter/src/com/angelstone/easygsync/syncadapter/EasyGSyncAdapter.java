package com.angelstone.easygsync.syncadapter;

import java.util.Date;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

/**
 * SyncAdapter implementation for syncing sample SyncAdapter contacts to the
 * platform ContactOperations provider.
 */
public class EasyGSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "SyncAdapter";

    private final AccountManager mAccountManager;
    private final Context mContext;

    private Date mLastUpdated;

    public EasyGSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
        mAccountManager = AccountManager.get(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
        ContentProviderClient provider, SyncResult syncResult) {
//        List<User> users;
//        List<Status> statuses;
//        String authtoken = null;
//         try {
//             // use the account manager to request the credentials
//             authtoken =
//                mAccountManager.blockingGetAuthToken(account,
//                    Constants.AUTHTOKEN_TYPE, true /* notifyAuthFailure */);
//             // fetch updates from the sample service over the cloud
//             users =
//                NetworkUtilities.fetchFriendUpdates(account, authtoken,
//                    mLastUpdated);
//            // update the last synced date.
//            mLastUpdated = new Date();
//            // update platform contacts.
//            Log.d(TAG, "Calling contactManager's sync contacts");
//            ContactManager.syncContacts(mContext, account.name, users);
//            // fetch and update status messages for all the synced users.
//            statuses = NetworkUtilities.fetchFriendStatuses(account, authtoken);
//            ContactManager.insertStatuses(mContext, account.name, statuses);
//        } catch (final AuthenticatorException e) {
//            syncResult.stats.numParseExceptions++;
//            Log.e(TAG, "AuthenticatorException", e);
//        } catch (final OperationCanceledException e) {
//            Log.e(TAG, "OperationCanceledExcetpion", e);
//        } catch (final IOException e) {
//            Log.e(TAG, "IOException", e);
//            syncResult.stats.numIoExceptions++;
//        } catch (final AuthenticationException e) {
//            mAccountManager.invalidateAuthToken(Constants.ACCOUNT_TYPE,
//                authtoken);
//            syncResult.stats.numAuthExceptions++;
//            Log.e(TAG, "AuthenticationException", e);
//        } catch (final ParseException e) {
//            syncResult.stats.numParseExceptions++;
//            Log.e(TAG, "ParseException", e);
//        } catch (final JSONException e) {
//            syncResult.stats.numParseExceptions++;
//            Log.e(TAG, "JSONException", e);
//        }
    }
}
