/**
 * Copyright 2022 P Hackathon
 * Licensed under the Apache License, Version 2.0 (the `License`);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an `AS IS` BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// [START gae_python38_log]
// [START gae_python3_log]
'use strict';

window.addEventListener('load', function () {

  console.log("Hello World!");

});
// [END gae_python3_log]
// [END gae_python38_log]

function edit(participant) {
  document.getElementsByName('name').forEach(element => element.value = participant.name);
  document.getElementsByName('comment').forEach(element => element.value = participant.comment);
  for (const i in participant.available_days) {
    let elements = document.getElementsByName(`radio_${i}`);
    switch (participant.available_days[i]) {
      case 1: elements[0].checked = true; break;
      case 0: elements[1].checked = true; break;
      case -1: default: elements[2].checked = true; break;
    }
  }
}
