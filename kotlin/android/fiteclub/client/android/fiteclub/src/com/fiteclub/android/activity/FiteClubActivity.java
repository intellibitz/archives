package com.fiteclub.android.activity;


/*
<!--
    Copyright (C) 2008 http://mobeegal.in

       All Rights Reserved.

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
-->
*/
/*
<!--
$Id::                                                                           $: Id of last commit
$Rev::                                                                          $: Revision of last commit
$Author::                                                                       $: Author of last commit
$Date::                                                                         $: Date of last commit
-->
*/

import android.app.Activity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.content.Intent;
import com.fiteclub.android.R;

abstract public class FiteClubActivity extends Activity
{

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        switch (item.getItemId()) {
            case R.id.menu_fight:
                intent.setType(getResources().getString(R.string.fight_mime_type));
                break;
            case R.id.menu_profile:
                intent.setType(getResources().getString(R.string.profile_mime_type));
                break;
            case R.id.menu_rankings:
                intent.setType(getResources().getString(R.string.rankings_mime_type));
                break;
        }
        startActivity(intent);
        return true;
    }

}