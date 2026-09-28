package com.fiteclub.android.activity

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

import android.app.Activity
import android.content.Intent
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.fiteclub.android.R

abstract class FiteClubActivity : Activity() {

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        when (item.itemId) {
            R.id.menu_fight ->
                intent.type = resources.getString(R.string.fight_mime_type)
            R.id.menu_profile ->
                intent.type = resources.getString(R.string.profile_mime_type)
            R.id.menu_rankings ->
                intent.type = resources.getString(R.string.rankings_mime_type)
        }
        startActivity(intent)
        return true
    }
}
