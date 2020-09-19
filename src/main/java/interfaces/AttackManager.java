package interfaces;

import models.Entity;

public interface AttackManager extends ArenaListener
{
    Entity handleAttack();
}
