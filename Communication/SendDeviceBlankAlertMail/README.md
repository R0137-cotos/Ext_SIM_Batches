# BatcheTemplate�ɂ���
�V���Ƀo�b�`���쐬����ꍇ�́A��PRJ���R�s�[���ăo�b�`���쐬���Ă��������B  

# �e���v���[�g�\���T�v
JobComponent->BatchComponent->BatchStepComponent�̊e���\�b�h���Ăяo���悤�\������Ă���B

���ތʏ����ɂ��ẮABatchStepComponent��DynamicProductConfig�o�R��BatchComponent�Ő؂�ւ������{����B

�e���\�b�h�̈����A�߂�l�ɂ��ẮA��������o�b�`�ɉ����ēK�X�ύX���邱�ƁB

�܂��A���ސؑւ����݂��Ȃ��ꍇ�͒萔�̍폜�ABatchStepComponentXXX�̍폜�ADynamicProductConfig���̐ؑփ��\�b�h�̓��e��K�X�ύX���邱�ƁB

# �e���v���[�g�\���ڍ�
## JobComponent
�P��BatchComponent���Ăяo���B

BatchComponent�ȉ��Ŕ��������O�͂��ׂĂ����ŃL���b�`����悤�ɂ��A��ʂɗ�O���X���[���Ȃ����ƁB

�P�̃e�X�g�F����n�A�ُ�n�̂Q�p�^�[��

## BatchComponent
���ސؑ֏������BatchStepComponent��check, afterProcess, process, beforeProcess���Ăяo���B

�P�̃e�X�g�FC0�p�^�[�� �~ �ؑ։\���ސ�

## DynamicProductConfig
BatchComponent���ɏ��ސؑփ��\�b�h����������B

���ސؑւ𔻒f����p�����[�^�[�i��F���i�}�X�^�̏��i��ދ敪�j�̓o�b�`���ɈقȂ�z��Ȃ̂ŁA�K�X���\�b�h�̏������e��ύX���邱�ƁB

## BatchStepComponent
�W���FIBatchStepComponent��implements���A�e���\�b�h����������B

���ތʁFBatchStepComponent��extends���A���ތʏ������K�v�ȃ��\�b�h�̂�Override����B

### check
�p�����[�^�[�`�F�b�N�����{����B

�P�̃e�X�g�FC0�x�[�X�Ńe�X�g

### afterProcess
INPUT�t�@�C���̓ǂݍ��݁ADB���珈���ɕK�v�ȃf�[�^���擾����B

�P�̃e�X�g�FC0�x�[�X�Ńe�X�g

### process
�ǂݍ��񂾃f�[�^�����H����B

�P�̃e�X�g�FC0�x�[�X�Ńe�X�g

### beforeProcess
�t�@�C���̏o�́ADB�ɏ����f�[�^���������݁i�X�e�[�^�X�X�V�Ȃǁj���s���B

�P�̃e�X�g�FC0�x�[�X�Ńe�X�g