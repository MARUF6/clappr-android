import sys
import time
from repository_manager import release_version_regex, snapshot_version_regex, bintray_upload, get_gradle_version, from_release_to_clappr_dir, from_clappr_to_release_dir, replace_string_on_gradle
from command_manager import print_error, execute_stage, print_success, run_tests
from git_manager import checkout_remote_branch, get_current_branch

target_branch = None


def search_snapshot_branch():
    print("branch=%s" % target_branch)
    current_branch = get_current_branch()
    if target_branch is not None and target_branch != "" and target_branch != current_branch:
        return checkout_remote_branch(target_branch)

    return True


def update_gradle_version():
    version = get_gradle_version(release_version_regex)
    if version == "":
        print_error("Wrong release version format")
        sys.exit(1)

    branch_name = get_current_branch()
    new_version = version + '-SNAP-' + branch_name.replace('/', '-') + '-' + time.strftime('%Y%m%d%H%M%S')
    return replace_string_on_gradle(version, new_version)


def verify_snapshot_pre_requisites():
    version = get_gradle_version(snapshot_version_regex)
    if version == "":
        print_error("Wrong version format")
        sys.exit(1)

    return True


if __name__ == '__main__':
    print('Starting snapshot process')

    stages = {
        'snapshot_branch': [search_snapshot_branch, update_gradle_version, verify_snapshot_pre_requisites],
        'run_unit_tests': [verify_snapshot_pre_requisites, run_tests],
        'publish_bintray': [verify_snapshot_pre_requisites, bintray_upload]
    }

    if len(sys.argv) < 2:
        print_error("Wrong number of arguments")
        sys.exit(1)

    if len(sys.argv) == 3:
        target_branch = sys.argv[2]

    print('Changing to clappr dir')
    from_release_to_clappr_dir()

    stage = sys.argv[1]
    execute_stage(stages, stage)

    print('Changing back to release dir')
    from_clappr_to_release_dir()

    print_success()